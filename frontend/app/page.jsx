import Hero from "./components/hero"
import Cards from "./components/cards"

export default function Home() {
  return (
    <>
      <div className="container" style={{maxWidth: 960}}>
        <Hero />
        <Cards />
      </div>
    </>
  );
}
